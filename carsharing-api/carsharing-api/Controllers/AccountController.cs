using System.Net;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using carsharing_api.Context;
using carsharing_api.Entities;
using JWT.Algorithms;
using JWT.Builder;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace carsharing_api.Controllers;

[Route("/api/account")]
public class AccountController : Controller
{
    private CarsharingDbContext _db;

    public AccountController(CarsharingDbContext db)
    {
        _db = db;
    }

    [HttpPost]
    [Route("/api/account/signup")]
    public IActionResult SignUp()
    {
        var email = Request.Form["email"].ToString();

        if (_db.User.Any(x => x.Email == email))
        {
            return new JsonResult(new { message = "Пользователь уже существует", status_code = 409 });
        }
        
        var password = Request.Form["password"].ToString();
        
        var user = new User()
        {
            Email = email,
            Password = password
        };

        _db.User.Add(user);
        _db.SaveChanges();
        
        var token = JwtBuilder.Create()
            .WithAlgorithm(new RS256Algorithm(GetCertificate("CN=CarsharingCert")))
            .AddClaim("user", EncryptDataWithAes(JsonConvert.SerializeObject(_db.User.Where(x => x.Email == email && x.Password == password).ToList()[0]), "etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g=", "autI78dTryrVFHHivDxr5g=="))
            .Encode();
        
        return new JsonResult(new { token, message = "Пользователь успешно зарегистрирован", status_code = 200 });
    }

    [HttpPost]
    [Route("/api/account/signin")]
    public IActionResult SignIn(string? code)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var oldToken = Request.Headers.Authorization.ToString()[7..];
            Console.WriteLine(oldToken);
            return VerifyToken(oldToken) 
                ? new JsonResult(new { token = RefreshToken(oldToken) }) 
                : new JsonResult(new { message = "Неверный токен", status_code = 401 });
        }

        var email = Request.Form["email"].ToString();
        
        if (code is not null)
        {
            var user = _db.User.Where(x => x.Email == email).ToList()[0];
            
            if (!_db.EmailCode.Any(x => x.UserId == user.Id && x.Code == code)) 
                return new JsonResult(new { message = "Неверный код", status_code = 409 });
            
            var tokenForCode = JwtBuilder.Create()
                .WithAlgorithm(new RS256Algorithm(GetCertificate("CN=CarsharingCert")))
                .AddClaim("user", EncryptDataWithAes(JsonConvert.SerializeObject(user), "etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g=", "autI78dTryrVFHHivDxr5g=="))
                .Encode();
            
            return new JsonResult(new { token = tokenForCode, message = "Успешный вход", status_code = 200 });
        }

        if (!_db.User.Any(x => x.Email == email))
        {
            return new JsonResult(new { message = "Пользователь не существует", status_code = 409 });
        }
        
        var password = Request.Form["password"].ToString();
        
        if (!_db.User.Any(x => x.Email == email && x.Password == password))
        {
            return new JsonResult(new { message = "Неверный пароль", status_code = 409 });
        }

        var token = JwtBuilder.Create()
            .WithAlgorithm(new RS256Algorithm(GetCertificate("CN=CarsharingCert")))
            .AddClaim("user", EncryptDataWithAes(JsonConvert.SerializeObject(_db.User.Where(x => x.Email == email && x.Password == password).ToList()[0]), "etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g=", "autI78dTryrVFHHivDxr5g=="))
            .Encode();
        
        return new JsonResult(new { token, message = "Успешный вход", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/account/upload")]
    public IActionResult UploadFile(string type, string token)
    {
        if (!VerifyToken(token))
        {
            return StatusCode(409);
        }

        var user = DecryptToken(token);
        
        var path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, type,
            Request.Form.Files[0].FileName.Replace("jpeg", "jpg"));

        if (!Directory.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, type)))
        {
            Directory.CreateDirectory(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, type));
        }
        
        using (var file = new StreamWriter(path))
        {
            Request.Form.Files[0].CopyTo(file.BaseStream);
            file.Flush();

            switch (type)
            {
                case "passport":
                {
                    var passport = new Passport()
                    {
                        Path = Request.Form.Files[0].FileName.Replace("jpeg", "jpg")
                    };

                    _db.Passport.Add(passport);
                    _db.SaveChanges();

                    var PassportId = _db.Passport.OrderBy(x => x.Id).Last().Id;
                    user.PassportId = PassportId;
                    _db.User.Update(user);
                    _db.SaveChanges();
                    
                    break;
                }
                case "driver_license":
                {
                    var license = new DriverLicense()
                    {
                        Path = Request.Form.Files[0].FileName.Replace("jpeg", "jpg")
                    };

                    _db.DriverLicense.Add(license);
                    _db.SaveChanges();
                    
                    var DriverLicenseId = _db.DriverLicense.OrderBy(x => x.Id).Last().Id;
                    user.DriverLicenseId = DriverLicenseId;
                    _db.User.Update(user);
                    _db.SaveChanges();
                    
                    break;
                }
                case "selfie":
                    var selfie = new Selfie()
                    {
                        Path = Request.Form.Files[0].FileName.Replace("jpeg", "jpg")
                    };

                    _db.Selfie.Add(selfie);
                    _db.SaveChanges();
                    
                    var SelfieId = _db.Selfie.OrderBy(x => x.Id).Last().Id;
                    user.SelfieId = SelfieId;
                    _db.User.Update(user);
                    _db.SaveChanges();
                    
                    break;
                default:
                    return StatusCode(409);
            }
        }
        
        return StatusCode(200);
    }

    [HttpPost]
    [Route("/api/account/generate_code")]
    public IActionResult GenerateCode(string token)
    {
        //TODO: Отправка на почту
        if (!VerifyToken(token))
        {
            return new JsonResult(new { message = "Неверный токен", status_code = 409 });
        }

        var user = DecryptToken(token);

        if (_db.EmailCode.Any(x => x.UserId == user.Id))
        {
            var record = _db.EmailCode.Where(x => x.UserId == user.Id).ToList()[0];
            
            if (DateTime.UtcNow - record.DateTime < TimeSpan.FromMinutes(5))
            {
                return new JsonResult(new { message = "Прошло менее 5 минут", status_code = 409 });
            }

            _db.EmailCode.Remove(record);
        }
        
        _db.EmailCode.Add(new EmailCode()
        {
            Code = new Random().Next(99999).ToString().PadLeft(5, '0'),
            DateTime = DateTime.UtcNow,
            UserId = user.Id
        });
        _db.SaveChanges();
        return new JsonResult(new { message = "Сообщение отправлено", status_code = 200 });
    }

    [HttpGet]
    [Route("/api/account/history/get")]
    public IActionResult GetActionHistory()
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
        
        Console.WriteLine(token);
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var logs = _db.TransportLog.Where(x => x.UserId == user.Id).ToList();
            
        return new ContentResult()
        {
            Content = JsonConvert.SerializeObject(logs.OrderBy(x => x.Id)),
            ContentType = "application/json"
        };
    }

    private bool VerifyToken(string token)
    {
        var user = DecryptToken(token);

        return _db.User.Any(x => x.Email == user.Email && x.Password == user.Password);
    }

    private string RefreshToken(string oldToken)
    {
        var user = DecryptToken(oldToken);
        
        var newToken = JwtBuilder.Create()
            .WithAlgorithm(new RS256Algorithm(GetCertificate("CN=CarsharingCert")))
            .AddClaim("user", EncryptDataWithAes(JsonConvert.SerializeObject(_db.User.Where(x => x.Id == user.Id).ToList()[0]), "etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g=", "autI78dTryrVFHHivDxr5g=="))
            .Encode();

        return newToken;
    }

    private User DecryptToken(string token)
    {
        var json = JwtBuilder.Create()
            .WithAlgorithm(new RS256Algorithm(GetCertificate("CN=CarsharingCert")))
            .MustVerifySignature()
            .Decode(token);

        var userText = DecryptDataWithAes(JObject.FromObject(JsonConvert.DeserializeObject(json))
                .GetValue("user").Value<string>(),
            "etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g=",
            "autI78dTryrVFHHivDxr5g==");

        var user = JsonConvert.DeserializeObject<User>(userText);

        return user!;
    }

    private X509Certificate2 GetCertificate(string certName)
    {
        X509Store store = new X509Store(StoreLocation.CurrentUser);
        try
        {
            store.Open(OpenFlags.ReadOnly);

            X509Certificate2Collection certCollection = store.Certificates;
            X509Certificate2Collection currentCerts = certCollection.Find(X509FindType.FindByTimeValid, DateTime.Now, false);
            X509Certificate2Collection signingCert = currentCerts.Find(X509FindType.FindBySubjectDistinguishedName, certName, false);
            if (signingCert.Count == 0)
            {
                return null;
            }
            return signingCert[0];
        }
        finally
        {
            store.Close();
        }
    }
    
    private string EncryptDataWithAes(string plainText, string keyBase64, string vectorBase64)
    {
        using (Aes aesAlgorithm = Aes.Create())
        {
            aesAlgorithm.Key = Convert.FromBase64String(keyBase64);
            aesAlgorithm.IV = Convert.FromBase64String(vectorBase64);

            ICryptoTransform encryptor = aesAlgorithm.CreateEncryptor();

            byte[] encryptedData;

            using (MemoryStream ms = new MemoryStream())
            {
                using (CryptoStream cs = new CryptoStream(ms, encryptor, CryptoStreamMode.Write))
                {
                    using (StreamWriter sw = new StreamWriter(cs))
                    {
                        sw.Write(plainText);
                    }
                    encryptedData = ms.ToArray();
                }
            }

            return Convert.ToBase64String(encryptedData);
        }
    }
    
    private static string DecryptDataWithAes(string cipherText, string keyBase64, string vectorBase64)
    {
        using (Aes aesAlgorithm = Aes.Create())
        {
            aesAlgorithm.Key = Convert.FromBase64String(keyBase64);
            aesAlgorithm.IV = Convert.FromBase64String(vectorBase64);

            ICryptoTransform decryptor = aesAlgorithm.CreateDecryptor();

            byte[] cipher = Convert.FromBase64String(cipherText);

            using (MemoryStream ms = new MemoryStream(cipher))
            {
                using (CryptoStream cs = new CryptoStream(ms, decryptor, CryptoStreamMode.Read))
                {
                    using (StreamReader sr = new StreamReader(cs))
                    {
                        return sr.ReadToEnd();
                    }
                }
            }
        }
    }
}