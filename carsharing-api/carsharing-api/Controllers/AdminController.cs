using System.Runtime.InteropServices;
using System.Security.Cryptography.X509Certificates;
using carsharing_api.Context;
using carsharing_api.Entities;
using JWT.Algorithms;
using JWT.Builder;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

namespace carsharing_api.Controllers;


[Route("/api/admin")]
public class AdminController: Controller
{
    private CarsharingDbContext _db;

    public AdminController(CarsharingDbContext db)
    {
        _db = db;
    }
    
    [HttpPost]
    [Route("/api/admin/signin")]
    public IActionResult AdminSignIn()
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var oldToken = Request.Headers.Authorization.ToString()[7..];
            return AccountController.VerifyToken(oldToken, _db) 
                ? new JsonResult(new { token = AccountController.RefreshToken(oldToken, _db), message = "Токен обновлён", status_code = 200 }) 
                : new JsonResult(new { message = "Неверный токен", status_code = 401 });
        }

        var email = Request.Form["email"].ToString();
        
        if (!_db.User.Any(x => x.Email == email))
        {
            return new JsonResult(new { message = "Пользователь не существует", status_code = 409 });
        }
        
        var user = _db.User.Where(x => x.Email == email).ToList()[0];
        var password = Request.Form["password"].ToString();
        
        if (email.Length == 0 || password.Length == 0) 
            return new JsonResult(new {message = "Не все поля заполнены", status_code = 409});
        
        if (!_db.User.Any(x => x.Email == email && x.Password == password))
        {
            return new JsonResult(new { message = "Неверный пароль", status_code = 409 });
        }
        
        if (user.UserRole != Role.ADMIN)
            return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });

        var token = JwtBuilder.Create()
            .WithAlgorithm(new RS256Algorithm(
                RuntimeInformation.IsOSPlatform(OSPlatform.Linux) 
                    ? new X509Certificate2("cert.pfx", "123")
                    : AccountController.GetCertificate("CN=CarsharingCert")
            ))
            .AddClaim("user", AccountController.EncryptDataWithAes(JsonConvert.SerializeObject(user), "etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g=", "autI78dTryrVFHHivDxr5g=="))
            .Encode();
        
        return new JsonResult(new { token, message = "Успешный вход", status_code = 200 });
    }

    [HttpGet]
    [Route("/api/admin/users/get")]
    public IActionResult GetUsers()
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var user = AccountController.DecryptToken(token);

            if (user.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });
            
            return new JsonResult(_db.User.ToList().Where(x => x.UserRole != Role.ADMIN));
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }
}