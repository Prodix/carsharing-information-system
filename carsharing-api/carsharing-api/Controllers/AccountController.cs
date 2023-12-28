using System.Net;
using System.Net.Mail;
using System.Security.Cryptography;
using System.Text;
using carsharing_api.Database;
using carsharing_api.Database.Models;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json.Linq;

namespace carsharing_api.Controllers;

[ApiController]
public class AccountController : ControllerBase
{
    private CarsharingDatabase _db;

    public AccountController(CarsharingDatabase db)
    {
        _db = db;
    }
    
    [HttpPost]
    [Route("/api/account/signUp")]
    public async Task<JsonResult> SignUp()
    {
        string body;

        using (StreamReader reader
                  = new StreamReader(HttpContext.Request.Body, Encoding.UTF8, true, 2048, true))
        {
            body = await reader.ReadToEndAsync();
        }


        var passport = JObject.Parse(body)["passport"].ToObject<Passport>();
        var license = JObject.Parse(body)["license"].ToObject<DriverLicense>();
        var user = JObject.Parse(body)["user"].ToObject<User>();

        if (_db.user.Any(x => x.Email == user.Email))
            return new JsonResult(new { message = "Пользователь уже зарегистрирован", status_code = 401 });
        
        if (_db.passport.Any(x => x.Serie == passport.Serie && x.Number == passport.Number))
            return new JsonResult(new { message = "Паспорт уже используется", status_code = 402 });
        
        if (_db.driver_license.Any(x => x.Serie == license.Serie && x.Number == license.Number))
            return new JsonResult(new { message = "Удостоверение уже используется", status_code = 403 });

        var md5 = MD5.Create();

        user.Password = Convert.ToBase64String(md5.ComputeHash(Encoding.UTF8.GetBytes(user.Password ?? string.Empty))).ToLower();
        user.Token = Convert.ToBase64String(md5.ComputeHash(Encoding.UTF8.GetBytes(user.Password + user.Email + DateTime.Now))).ToLower();
        
        await _db.AddAsync(passport);
        await _db.AddAsync(license);
        await _db.AddAsync(new Billing
        {
            Balance = 0
        });

        await _db.SaveChangesAsync();

        user.Billing = _db.billing.ToList().Last().Id;
        user.Passport = _db.passport.ToList().Last().Id;
        user.DriverLicense = _db.driver_license.ToList().Last().Id;
        await _db.AddAsync(user);

        await _db.SaveChangesAsync();
        
        return new JsonResult(new {message = "Успешная регистрация", status_code = 200});
    }
    
    [HttpPost]
    [Route("/api/account/signIn")]
    public async Task<JsonResult> SignIn(string password, string email)
    {
        if (!_db.user.Any(x => x.Email == email))
            return new JsonResult(new { message = "Пользователя не существует", status_code = 400 });

        var md5 = MD5.Create();
        
        if (!_db.user.Any(x => x.Email == email && x.Password == Convert.ToBase64String(md5.ComputeHash(Encoding.UTF8.GetBytes(password))).ToLower()))
            return new JsonResult(new { message = "Неверный пароль", status_code = 400 });


        var user = _db.user.First(x => x.Email == email && x.Password == Convert.ToBase64String(md5.ComputeHash(Encoding.UTF8.GetBytes(password))).ToLower());
        
        
        user.Token = Convert.ToBase64String(md5.ComputeHash(Encoding.UTF8.GetBytes(password + email + DateTime.Now))).ToLower();
        
        _db.Update(user);

        await _db.SaveChangesAsync();
        
        return new JsonResult(new {message = "Успешный вход", status_code = 200});
    }
    
    [HttpPost]
    [Route("/api/account/changePassword")]
    public async Task<JsonResult> ChangePassword(string password, string token)
    {
        if (!_db.user.Any(x => x.Token == token))
            return new JsonResult(new { message = "Пользователя не существует", status_code = 400 });

        var user = _db.user.First(x => x.Token == token);
        user.Password = password;
        
        var md5 = MD5.Create();
        
        user.Token = Convert.ToBase64String(md5.ComputeHash(Encoding.UTF8.GetBytes(password + user.Email + DateTime.Now))).ToLower();
        
        _db.Update(user);

        await _db.SaveChangesAsync();
        
        return new JsonResult(new {message = "Успешная смена пароля", status_code = 200});
    }
    
    [HttpPost]
    [Route("/api/account/sendEmailCode")]
    public async Task<JsonResult> SendEmailCode(string email)
    {
        var fromAddress = new MailAddress("flgodd@yandex.ru", "Техподдержка AutoShare");
        var toAddress = new MailAddress(email);
        var random = new Random();
        var code = random.Next(0, 99999);
            
        const string fromPassword = "password";
        const string subject = "Подтверждение регистрации AutoShare";
        var body = $"Здравствуйте,\nВы получаете это сообщение, потому что начали регистрацию аккаунта в каршеринге AutoShare.\nДля подтверждения регистрации, используйте этот код: {code.ToString().PadLeft(4, '0')}\nЕсли вы не регистрировались, пожалуйста, проигнорируйте это сообщение.\nБлагодарим вас за использование AutoShare. Если у вас возникли дополнительные вопросы или проблемы с доступом к вашему аккаунту, не стесняйтесь связаться с нашей службой поддержки по адресу flgodd@yandex.ru.\nС уважением, Команда AutoShare";

        var smtp = new SmtpClient
        {
            Host = "smtp.yandex.ru",
            Port = 587,
            EnableSsl = true,
            DeliveryMethod = SmtpDeliveryMethod.Network,
            UseDefaultCredentials = false,
            Credentials = new NetworkCredential(fromAddress.Address, fromPassword)
        };

        using (var message = new MailMessage(fromAddress, toAddress))
        {
            message.Subject = subject;
            message.Body = body;
            smtp.Send(message);
        }


        if (_db.codes.Any(x => x.Email == email))
            _db.codes.Remove(_db.codes.First(x => x.Email == email));
        
        await _db.codes.AddAsync(new Code
        {
            CreationDate = DateTime.Now,
            Email = email,
            GeneratedCode = code
        });

        await _db.SaveChangesAsync();
        
        return new JsonResult(new { message = "Код отправлен", status_code = 200 });
    }
    
    [HttpPost]
    [Route("/api/account/validateEmailCode")]
    public async Task<JsonResult> ValidateEmailCode(int code, string email)
    {
        if (!_db.codes.Any(x => x.Email == email && x.GeneratedCode == code))
            return new JsonResult(new { message = "Неверный код", status_code = 400 });

        _db.codes.Remove(_db.codes.First(x => x.Email == email));

        await _db.SaveChangesAsync();
        
        return new JsonResult(new { message = "Код верный", status_code = 200 });
    }
}