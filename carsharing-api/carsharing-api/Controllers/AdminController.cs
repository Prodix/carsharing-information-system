using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using carsharing_api.Context;
using carsharing_api.Entities;
using JWT.Algorithms;
using JWT.Builder;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using NPOI.HSSF.Util;
using NPOI.SS.UserModel;
using NPOI.SS.Util;
using NPOI.XSSF.UserModel;
using NtpClient;

namespace carsharing_api.Controllers;

class RateEqualityComparer : IEqualityComparer<Rate>
{
    public bool Equals(Rate x, Rate y)
    {
        if (ReferenceEquals(x, y)) return true;
        if (ReferenceEquals(x, null)) return false;
        if (ReferenceEquals(y, null)) return false;
        if (x.GetType() != y.GetType()) return false;
        return x.Id == y.Id;
    }

    public int GetHashCode(Rate obj)
    {
        return obj.Id ^ obj.TransportId ^ Convert.ToInt32(obj.OnRoadPrice) ^ Convert.ToInt32(obj.ParkingPrice);
    }
}

class FunctionEqualityComparer : IEqualityComparer<Function>
{
    public bool Equals(Function x, Function y)
    {
        if (ReferenceEquals(x, y)) return true;
        if (ReferenceEquals(x, null)) return false;
        if (ReferenceEquals(y, null)) return false;
        if (x.GetType() != y.GetType()) return false;
        return x.Id == y.Id;
    }

    public int GetHashCode(Function obj)
    {
        return obj.Id ^ obj.TransportId;
    }
}


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
        var password = Convert.ToBase64String(MD5.Create().ComputeHash(Encoding.UTF8.GetBytes(Request.Form["password"].ToString()))).ToLower();

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
    
    [HttpGet]
    [Route("/api/admin/users/get/document")]
    public async Task GetUsersDocument()
    {
        var fileName = $"{DateTime.UtcNow.Ticks}.xlsx";
            
        using (var fs = new FileStream(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, fileName), FileMode.Create, FileAccess.Write)) {
            IWorkbook workbook = new XSSFWorkbook();

            ISheet sheet1 = workbook.CreateSheet("Выгрузка");

            var users = _db.User.Where(x => x.UserRole != Role.ADMIN).ToList();
                
            for (int i = 0; i < users.Count; i++ )
            {
                IRow row = sheet1.CreateRow(i);
                if (i == 0)
                {
                    row.CreateCell(0).SetCellValue("Id");
                    row.CreateCell(1).SetCellValue("Почта");
                    row.CreateCell(2).SetCellValue("Роль");
                    row.CreateCell(3).SetCellValue("Баланс");
                    row.CreateCell(4).SetCellValue("Рейтинг");
                    row.CreateCell(5).SetCellValue("Подтвержден");
                    row.CreateCell(6).SetCellValue("Подтверждена почта");
                    sheet1.AutoSizeColumn(0);
                    sheet1.AutoSizeColumn(1);
                    sheet1.AutoSizeColumn(2);
                    sheet1.AutoSizeColumn(3);
                    sheet1.AutoSizeColumn(4);
                    sheet1.AutoSizeColumn(5);
                    sheet1.AutoSizeColumn(6);
                }
                else
                {
                    row.CreateCell(0).SetCellValue(users[i].Id);
                    row.CreateCell(1).SetCellValue(users[i].Email);
                    row.CreateCell(2).SetCellValue(users[i].UserRole == 0 ? "Пользователь" : "Админ");
                    row.CreateCell(3).SetCellValue(users[i].Balance);
                    row.CreateCell(4).SetCellValue(users[i].Rating);
                    row.CreateCell(5).SetCellValue(users[i].IsVerified);
                    row.CreateCell(6).SetCellValue(users[i].IsEmailVerified);
                }
            }
                
            workbook.Write(fs);
                
        }
        
        Task.Run(async () =>
        {
            await Task.Delay(TimeSpan.FromSeconds(30));
            System.IO.File.Delete(fileName);
        });
            
        Response.Headers.ContentDisposition = "attachment; filename=users.xlsx";
        await Response.SendFileAsync(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, fileName));
    }

    [HttpGet]
    [Route("/api/admin/transport/get/document")]
    public async Task GetTransportDocument()
    {
        var fileName = $"{DateTime.UtcNow.Ticks}.xlsx";
            
        using (var fs = new FileStream(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, fileName), FileMode.Create, FileAccess.Write)) {
            IWorkbook workbook = new XSSFWorkbook();

            ISheet sheet1 = workbook.CreateSheet("Выгрузка");

            var transport = _db.Transport.ToList();
                
            for (int i = 0; i < transport.Count; i++ )
            {
                if (i == 0)
                {
                    IRow row = sheet1.CreateRow(i);
                    row.CreateCell(0).SetCellValue("Id");
                    row.CreateCell(1).SetCellValue("Название");
                    row.CreateCell(2).SetCellValue("Номер");
                    row.CreateCell(3).SetCellValue("Страховка");
                    row.CreateCell(4).SetCellValue("Потребление топлива");
                    row.CreateCell(5).SetCellValue("Уровень топлива");
                    row.CreateCell(6).SetCellValue("Объём бака");
                    row.CreateCell(7).SetCellValue("Широта");
                    row.CreateCell(8).SetCellValue("Долгота");
                    row.CreateCell(9).SetCellValue("Зарезервирован");
                    row.CreateCell(10).SetCellValue("Категория");
                    sheet1.AutoSizeColumn(0);
                    sheet1.AutoSizeColumn(1);
                    sheet1.AutoSizeColumn(2);
                    sheet1.AutoSizeColumn(3);
                    sheet1.AutoSizeColumn(4);
                    sheet1.AutoSizeColumn(5);
                    sheet1.AutoSizeColumn(6);
                    sheet1.AutoSizeColumn(7);
                    sheet1.AutoSizeColumn(8);
                    sheet1.AutoSizeColumn(9);
                    sheet1.AutoSizeColumn(10);
                }
                IRow newRow = sheet1.CreateRow(i+1);
                newRow.CreateCell(0).SetCellValue(transport[i].Id);
                newRow.CreateCell(1).SetCellValue(transport[i].CarName);
                newRow.CreateCell(2).SetCellValue(transport[i].CarNumber);
                newRow.CreateCell(3).SetCellValue(transport[i].InsuranceType);
                newRow.CreateCell(4).SetCellValue(transport[i].GasConsumption);
                newRow.CreateCell(5).SetCellValue(transport[i].GasLevel);
                newRow.CreateCell(6).SetCellValue(transport[i].TankCapacity);
                newRow.CreateCell(7).SetCellValue(transport[i].Latitude);
                newRow.CreateCell(8).SetCellValue(transport[i].Longitude);
                newRow.CreateCell(9).SetCellValue(transport[i].IsReserved);
                newRow.CreateCell(10).SetCellValue(transport[i].TransportType == CarType.BASE ? "Базовый" : transport[i].TransportType == CarType.COMFORT ? "Комфорт" : "Бизнес");

            }
                
            workbook.Write(fs);
        }
        
        Task.Run(async () =>
        {
            await Task.Delay(TimeSpan.FromSeconds(30));
            System.IO.File.Delete(fileName);
        });
            
        Response.Headers.ContentDisposition = "attachment; filename=transport.xlsx";
        await Response.SendFileAsync(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, fileName));
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

    [HttpPost]
    [Route("/api/admin/transport/add")]
    public IActionResult AddTransport()
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var user = AccountController.DecryptToken(token);

            if (user.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });
            
            var lastTransportId = _db.Transport.ToList().OrderBy(x => x.Id).Last().Id;
            
            var transport = new Transport()
            {
                CarImagePath = "car.png",
                CarName = "car.png",
                CarNumber = "car.png",
                Functions = new List<Function>(),
                GasConsumption = 0,
                GasLevel = 0,
                InsuranceType = "",
                IsDoorOpened = false,
                IsReserved = false,
                Latitude = 0,
                Longitude = 0,
                TankCapacity = 0,
                Rates = new List<Rate>()
            };
            
            return new JsonResult(new {});
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }

    [HttpPost]
    [Route("/api/admin/transport/save")]
    public async Task<IActionResult> SaveTransport(int id)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var user = AccountController.DecryptToken(token);

            if (user.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });

            using var reader = new StreamReader(Request.Body);

            var text = await reader.ReadToEndAsync();
            var transport = JsonConvert.DeserializeObject<Transport>(text);
            
            Console.WriteLine(text);
            Console.WriteLine(transport.Rates.Count);
            
            if (_db.Transport.AsNoTracking().Where(x => x.CarNumber == transport.CarNumber && x.Id != transport.Id).ToList().Count != 0)
                return new JsonResult(new { message = "Транспорт с таким номером уже существует", status_code = 409 });
            
            if (id > 0)
            {
                var curTransport = _db.Transport.AsNoTracking().ToList().First(x => x.Id == id);
                curTransport.Rates = _db.Rate.AsNoTracking().Where(x => x.TransportId == id).ToList();
                curTransport.Functions = _db.Function.AsNoTracking().Where(x => x.TransportId == id).ToList();

                var exceptionsFromCurRates = curTransport.Rates.Except(transport.Rates, new RateEqualityComparer()).ToList();
                var exceptionsFromNewRates = transport.Rates.Except(curTransport.Rates, new RateEqualityComparer()).ToList();

                var exceptionsFromCurFunctions = curTransport.Functions.Except(transport.Functions, new FunctionEqualityComparer()).ToList();
                var exceptionsFromNewFunctions = transport.Functions.Except(curTransport.Functions, new FunctionEqualityComparer()).ToList();

            
                foreach (var rate in exceptionsFromCurRates)
                {
                    _db.Rate.Remove(rate);
                    await _db.SaveChangesAsync();
                }
            
                foreach (var rate in exceptionsFromNewRates)
                {
                    rate.Id = 0;
                    rate.TransportId = id;
                    _db.Rate.Add(rate);
                    await _db.SaveChangesAsync();
                }
            
                foreach (var function in exceptionsFromCurFunctions)
                {
                    _db.Function.Remove(function);
                    await _db.SaveChangesAsync();
                }
            
                foreach (var function in exceptionsFromNewFunctions)
                {
                    function.Id = 0;
                    function.TransportId = id;
                    _db.Function.Add(function);
                    await _db.SaveChangesAsync();
                }

                _db.Transport.Update(transport);
                await _db.SaveChangesAsync();
            }
            else
            {
                transport.Id = 0;
                _db.Transport.Add(transport);
                await _db.SaveChangesAsync();
            }
            
            return new JsonResult(new { message = "Транспорт сохранен", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }

    [HttpPost]
    [Route("/api/admin/transport/delete")]
    public IActionResult DeleteTransport(int id)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var user = AccountController.DecryptToken(token);

            if (user.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });

            var transport = _db.Transport.AsNoTracking().ToList().First(x => x.Id == id);
            _db.Transport.Remove(transport);
            _db.SaveChanges();
            
            return new JsonResult(new { message = "Транспорт сохранен", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }

    [HttpPost]
    [Route("/api/admin/transport/image/add")]
    public IActionResult AddTransportImage(int id)
    {
        var file = Request.Form.Files[0];
        var fileName = (DateTime.UtcNow.ToUniversalTime().Ticks + new Random().Next()).ToString() + ".png";
        using var stream = System.IO.File.Create($"{AppDomain.CurrentDomain.BaseDirectory}/images/{fileName}");
        file.CopyTo(stream);
        stream.Flush();
        var transport = _db.Transport.AsNoTracking().ToList().First(x => x.Id == id);
        transport.CarImagePath = fileName;
        _db.Transport.Update(transport);
        _db.SaveChanges();
        return new JsonResult(new { message = "Изображение сохранено", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/admin/damage/delete")]
    public IActionResult DeleteDamage(string name)
    {
        if (Request.Headers.ContainsKey("Authorization"))
        {
            var token = Request.Headers.Authorization.ToString()[7..];
            if (!AccountController.VerifyToken(token, _db))
                return new JsonResult(new { message = "Неверный токен", status_code = 401 });

            var user = AccountController.DecryptToken(token);

            if (user.UserRole != Role.ADMIN)
                return new JsonResult(new { message = "Недостаточно прав", status_code = 409 });

            var damageToDelete = _db.Damage.First(x => x.Path == name);
            
            System.IO.File.Delete(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "damages", damageToDelete.Path)); 
            
            _db.Damage.Remove(damageToDelete);
            _db.SaveChanges();
            
            return new JsonResult(new { message = "Повреждение удалено", status_code = 200 });
        }
        
        return new JsonResult(new { message = "Неверный токен", status_code = 401 });
    }
    
    
}