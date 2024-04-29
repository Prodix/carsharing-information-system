using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using carsharing_api.Context;
using carsharing_api.Entities;
using JWT.Algorithms;
using JWT.Builder;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;
using Npgsql;
using NtpClient;
using Action = carsharing_api.Entities.Action;

namespace carsharing_api.Controllers;

[Route("/api/transport")]
public class TransportController : Controller
{
    private CarsharingDbContext _db;
    private NtpConnection _ntpClient;

    public TransportController(CarsharingDbContext db)
    {
        _db = db;
        _ntpClient = new NtpConnection("time.google.com");
    }

    [HttpGet]
    [Route("/api/transport/get")]
    public IActionResult GetTransport()
    {
        var settings = new JsonSerializerSettings();
        settings.Converters.Add(new StringEnumConverter());
        var transportList = _db.Transport.ToList();

        foreach (var transport in transportList)
        {
            transport.Functions = _db.Function.Where(x => x.TransportId == transport.Id).ToList();
            transport.Rates = _db.Rate.Where(x => x.TransportId == transport.Id).ToList();
        }
        
        return new ContentResult()
        {
            Content = JsonConvert.SerializeObject(transportList,  settings),
            ContentType = "application/json"
        };
    }
    
    [HttpGet]
    [Route("/api/transport/get/image")]
    public async Task GetTransportImage(string name)
    {
        Response.Headers.ContentDisposition = "attachment";
        await Response.SendFileAsync(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "images", name));
    }
    
    [HttpGet]
    [Route("/api/transport/get/damage")]
    public IActionResult GetTransportDamage(int id)
    {
        List<string> paths = _db.Damage.Where(x => x.TransportId == id)
            .Select(x => x.Path)
            .ToList();
        
        return new ContentResult()
        {
            Content = JsonConvert.SerializeObject(paths),
            ContentType = "application/json"
        };
    }

    [HttpPost]
    [Route("/api/transport/reserve")]
    public IActionResult ReserveTransport(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.RESERVE,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Транспорт успешно забронирован!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/cancel_reserve")]
    public IActionResult CancelReserve(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse();

        foreach (var record in log)
        {
            if (record.Action is Action.RESERVE)
            {
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.CHECK or Action.RENT)
            {
                return new JsonResult(new { message = "Вы не бронируете этот транспорт", status_code = 401 });
            }
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.CANCEL_RESERVE,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Бронирование успешно отменено!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/check")]
    public IActionResult StartCheck(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse();

        foreach (var record in log)
        {
            if (record.Action is Action.RESERVE)
            {
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.CHECK or Action.RENT)
            {
                return new JsonResult(new { message = "Вы не бронируете этот транспорт", status_code = 401 });
            }
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.CHECK,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Проверка успешно начата!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/rent")]
    public IActionResult RentCar(int transportId, int rateId, double? rentHours)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse();

        foreach (var record in log)
        {
            if (record.Action is Action.CHECK)
            {
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.RESERVE or Action.RENT)
            {
                return new JsonResult(new { message = "Вы не бронируете этот транспорт", status_code = 401 });
            }
        }

        var rate = _db.Rate.First(x => x.Id == rateId);

        if (rate.RateName == "Фикс")
        {
            _db.RentHistory.Add(new RentHistory()
            {
                TransportId = transportId,
                UserId = user.Id,
                RateId = rateId,
                RentTime = TimeSpan.FromHours((double)rentHours!),
                Price = rate.OnRoadPrice * (double)rentHours * 60
            });
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.RENT,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Транспорт успешно арендован!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/cancel_rent")]
    public IActionResult CancelRent(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse().ToList();

        var rentRecord = new TransportLog();

        foreach (var record in log)
        {
            if (record.Action is Action.RENT)
            {
                rentRecord = record;
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.RESERVE or Action.CHECK)
            {
                return new JsonResult(new { message = "Вы не арендуете этот транспорт", status_code = 401 });
            }
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.CANCEL_RENT,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        var rate = _db.Rate.First(x => x.Id == rateId);

        var currentTime = _ntpClient.GetUtc();
        
        var elapsedTime = Math.Abs(((DateTimeOffset)currentTime).ToUnixTimeSeconds() - ((DateTimeOffset)rentRecord.DateTime).ToUnixTimeSeconds());
        var parkingTime = 0L;
        var roadTime = 0L;

        if (log[0].Action == Action.LOCK)
        {
            parkingTime = elapsedTime;
            var @lock = 0L;
            for (int i = 0; i < log.Count; i++)
            {
                if (log[i].Action == Action.LOCK)
                {
                    @lock = ((DateTimeOffset)log[i].DateTime).ToUnixTimeSeconds();
                } else if (log[i].Action == Action.UNLOCK || log[i].Action == Action.RENT)
                {
                    @lock -= ((DateTimeOffset)log[i].DateTime).ToUnixTimeSeconds();
                    parkingTime -= @lock;
                }

                if (log[i].Action == Action.CANCEL_RENT || log[i].Action == Action.CANCEL_CHECK ||
                    log[i].Action == Action.CANCEL_RESERVE)
                {
                    break;
                }
            }
            roadTime = elapsedTime - parkingTime;
        }
        else if (log[0].Action == Action.UNLOCK)
        {
            roadTime = elapsedTime;
            var unlock = 0L;
            for (int i = 0; i < log.Count; i++)
            {
                if (log[i].Action == Action.UNLOCK)
                {
                    unlock = ((DateTimeOffset)log[i].DateTime).ToUnixTimeSeconds();
                } else if (log[i].Action == Action.LOCK)
                {
                    unlock -= ((DateTimeOffset)log[i].DateTime).ToUnixTimeSeconds();
                    roadTime -= unlock;
                }
                if (log[i].Action == Action.CANCEL_RENT || log[i].Action == Action.CANCEL_CHECK ||
                    log[i].Action == Action.CANCEL_RESERVE)
                {
                    break;
                }
            }
            parkingTime = elapsedTime - roadTime;
        }
        else if (log[0].Action == Action.RENT)
        {
            roadTime = elapsedTime;
        }

        var checkSeconds = 0L;
        
        for (int i = 0; i < log.Count; i++)
        {
            if (log[i].Action == Action.CHECK)
            {
                checkSeconds = ((DateTimeOffset)log[i - 1].DateTime).ToUnixTimeSeconds() -
                               ((DateTimeOffset)log[i].DateTime).ToUnixTimeSeconds() - 300;
                break;
            }
        }

        var rent = _db.Rate.First(x => x.Id == rateId);

        if (rate.RateName != "Фикс")
        {
            _db.RentHistory.Add(new RentHistory()
            {
                TransportId = transportId,
                RateId = rateId,
                UserId = user.Id,
                RentTime = currentTime - rentRecord.DateTime,
                Price = rate.OnRoadPrice * (roadTime / 60 + 1) + rate.ParkingPrice * (parkingTime / 60 + 1) + (checkSeconds > 0 ? checkSeconds / 60 + 1 : 0) * 10
            });

            _db.SaveChanges();
        }
        

        return new JsonResult(new { message = "Аренда успешно закончена!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/lock")]
    public IActionResult Lock(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse();

        foreach (var record in log)
        {
            if (record.Action is Action.RENT)
            {
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.RESERVE or Action.CHECK)
            {
                return new JsonResult(new { message = "Вы не арендуете этот транспорт", status_code = 401 });
            }
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.LOCK,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Транспорт успешно закрыт!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/unlock")]
    public IActionResult Unlock(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse();

        foreach (var record in log)
        {
            if (record.Action is Action.RENT or Action.RESERVE)
            {
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.CHECK)
            {
                return new JsonResult(new { message = "Вы не арендуете или не бронируете этот транспорт", status_code = 401 });
            }
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.UNLOCK,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Транспорт успешно открыт!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/beep")]
    public IActionResult Beep(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse().ToList();
        
        if (log.Any(x => x.Action == Action.BEEP) && ((DateTime.Now.ToUniversalTime()) - log.First(x => x.Action == Action.BEEP).DateTime).Minutes < 1)
        {
            return new JsonResult(new { message = "Подождите минуту чтобы посигналить снова", status_code = 401 });
        }

        if (log.Any(x => x.Action == Action.RESERVE) && log.Take(log.FindIndex(x => x.Action == Action.RESERVE)).Count(x => x.Action == Action.BEEP) >= 10)
        {
            _db.TransportLog.Add(new TransportLog()
            {
                Action = Action.CANCEL_RESERVE,
                DateTime = _ntpClient.GetUtc(),
                RateId = rateId,
                TransportId = transportId,
                UserId = user.Id
            });

            _db.SaveChanges();
            
            return new JsonResult(new { message = "Ваше бронирование отменено за постоянное нажатие на сигнал", status_code = 401 });
        } 
        
        foreach (var record in log)
        {
            if (record.Action is Action.RESERVE)
            {
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.CHECK or Action.RENT)
            {
                return new JsonResult(new { message = "Вы не бронируете этот транспорт", status_code = 401 });
            }
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.BEEP,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Транспорт посигналил!", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/transport/flash")]
    public IActionResult Flash(int transportId, int rateId)
    {
        if (!Request.Headers.ContainsKey("Authorization"))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
        
        var token = Request.Headers.Authorization.ToString()[7..];
            
        if (!VerifyToken(token))
            return new JsonResult(new { message = "Неверный токен", status_code = 401 });
            
        var user = DecryptToken(token);

        var log = _db.TransportLog.Where(x => x.TransportId == transportId && x.UserId == user.Id)
            .ToList().OrderBy(x => x.Id).Reverse().ToList();

        if (log.Any(x => x.Action == Action.FLASH) && ((DateTime.Now.ToUniversalTime()) - log.First(x => x.Action == Action.FLASH).DateTime).Minutes < 1)
        {
            return new JsonResult(new { message = "Подождите минуту чтобы моргнуть снова", status_code = 401 });
        }

        if (log.Any(x => x.Action == Action.RESERVE) && log.Take(log.FindIndex(x => x.Action == Action.RESERVE)).Count(x => x.Action == Action.FLASH) >= 10)
        {
            _db.TransportLog.Add(new TransportLog()
            {
                Action = Action.CANCEL_RESERVE,
                DateTime = _ntpClient.GetUtc(),
                RateId = rateId,
                TransportId = transportId,
                UserId = user.Id
            });

            _db.SaveChanges();
            
            return new JsonResult(new { message = "Ваше бронирование отменено за постоянное моргание фарами", status_code = 401 });
        } 

        foreach (var record in log)
        {
            if (record.Action is Action.RESERVE)
            {
                break;
            }
            
            if (record.Action is Action.CANCEL_RENT or Action.CANCEL_RESERVE or Action.RENT or Action.CHECK)
            {
                return new JsonResult(new { message = "Вы не бронируете этот транспорт", status_code = 401 });
            }
        }

        _db.TransportLog.Add(new TransportLog()
        {
            Action = Action.FLASH,
            DateTime = _ntpClient.GetUtc(),
            RateId = rateId,
            TransportId = transportId,
            UserId = user.Id
        });

        _db.SaveChanges();

        return new JsonResult(new { message = "Транспорт поморгал фарами!", status_code = 200 });
    }
    
    [HttpGet]
    [Route("/api/transport/get/damage/image")]
    public async Task GetTransportDamage(string name)
    {
        Response.Headers.ContentDisposition = "attachment";
        await Response.SendFileAsync(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "damages", name));
    }
    
    [HttpPost]
    [Route("/api/transport/damage/send")]
    public IActionResult TransportDamageUpload()
    {
        var path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "damages",
            Request.Form.Files[0].FileName.Replace("jpeg", "jpg"));
        
        using (var file = new StreamWriter(path))
        {
            Request.Form.Files[0].CopyTo(file.BaseStream);
            file.Flush();
            
            var damage = new Damage()
            {
                TransportId = Convert.ToInt32(Request.Form.Files[0].FileName[0].ToString()),
                Path = Request.Form.Files[0].FileName.Replace("jpeg", "jpg")
            };

            _db.Damage.Add(damage);
            _db.SaveChanges();
        }

        return StatusCode(200);
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