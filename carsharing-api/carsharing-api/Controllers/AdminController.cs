using System.Runtime.InteropServices;
using System.Security.Cryptography.X509Certificates;
using carsharing_api.Context;
using carsharing_api.Entities;
using JWT.Algorithms;
using JWT.Builder;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using NtpClient;

namespace carsharing_api.Controllers;


[Route("/api/admin")]
public class AdminController: Controller
{
    private CarsharingDbContext _db;
    private NtpConnection _ntpClient;

    public AdminController(CarsharingDbContext db)
    {
        _db = db;
        _ntpClient = new NtpConnection("time.google.com");
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

    [HttpPost]
    [Route("/api/admin/users/delete")]
    public IActionResult DeleteUser(int id)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var user = AccountController.DecryptToken(token);

            if (user.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });

            var userToDelete = _db.User.First(x => x.Id == id);
            
            var passport = _db.Passport.First(x => x.Id == userToDelete.PassportId);
            
            System.IO.File.Delete(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "passport", passport.Path)); 
            
            var selfie = _db.Selfie.First(x => x.Id == userToDelete.SelfieId);
            
            System.IO.File.Delete(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "selfie", selfie.Path)); 
            
            var driverLicense = _db.DriverLicense.First(x => x.Id == userToDelete.DriverLicenseId);
            
            System.IO.File.Delete(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "driver_license", driverLicense.Path)); 
            
            _db.User.Remove(userToDelete);
            _db.SaveChanges();
            
            _db.Passport.Remove(passport);
            _db.SaveChanges();
            
            _db.DriverLicense.Remove(driverLicense);
            _db.SaveChanges();
            
            _db.Selfie.Remove(selfie);
            _db.SaveChanges();
            
            return new JsonResult(new { message = "Пользователь удален", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }

    [HttpPost]
    [Route("/api/admin/users/verify")]
    public IActionResult VerifyUser(int id)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var admin = AccountController.DecryptToken(token);

            if (admin.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });
            
            var user = _db.User.First(x => x.Id == id);
            user.IsVerified = true;
            _db.User.Update(user);
            _db.SaveChanges();
            
            return new JsonResult(new { message = "Пользователь верифицирован", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }
    
    [HttpPost]
    [Route("/api/admin/users/unverify")]
    public IActionResult UnverifyUser(int id)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var admin = AccountController.DecryptToken(token);

            if (admin.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });
            
            var user = _db.User.First(x => x.Id == id);
            user.IsVerified = false;
            _db.User.Update(user);
            _db.SaveChanges();
            
            return new JsonResult(new { message = "Верицификация пользователя отменена", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }

    [HttpGet]
    [Route("/api/admin/users/get_history")]
    public IActionResult GetHistory(int id)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var admin = AccountController.DecryptToken(token);

            if (admin.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });
            
            var user = _db.User.First(x => x.Id == id);
            var history = _db.RentHistory.Where(x => x.UserId == user.Id).ToList();
            history.ForEach(x => x.Transport = _db.Transport.First(y => y.Id == x.TransportId));
            return new JsonResult(history.OrderByDescending(x => x.Date));
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }
    
    [HttpPost]
    [Route("/api/admin/users/give_penalty")]
    public async Task<IActionResult> GivePenalty()
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var admin = AccountController.DecryptToken(token);

            if (admin.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });

            using var reader = new StreamReader(Request.Body);

            var penalty = JsonConvert.DeserializeObject<Penalty>(await reader.ReadToEndAsync());
            
            _db.Penalty.Add(penalty);
            await _db.SaveChangesAsync();
            
            return new JsonResult(new { message = "Штраф выдан", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }

    [HttpPost]
    [Route("/api/admin/users/send_message")]
    public async Task<IActionResult> SendMessage(int id)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var admin = AccountController.DecryptToken(token);

            if (admin.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });

            using var reader = new StreamReader(Request.Body);

            var text = await reader.ReadToEndAsync();

            var message = new UserMessage()
            {
                Datetime = _ntpClient.GetUtc(),
                IsRead = false,
                Message = text,
                UserId = id
            };

            _db.Message.Add(message);
            
            await _db.SaveChangesAsync();
            
            return new JsonResult(new { message = "Сообщение отправлено", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }
}