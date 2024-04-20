using carsharing_api.Context;
using carsharing_api.Entities;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Npgsql;

namespace carsharing_api.Controllers;

[Route("/api/transport")]
public class TransportController : Controller
{
    private CarsharingDbContext _db;

    public TransportController(CarsharingDbContext db)
    {
        _db = db;
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
    
    [HttpGet]
    [Route("/api/transport/get/damage/image")]
    public async Task GetTransportDamage(string name)
    {
        Response.Headers.ContentDisposition = "attachment";
        await Response.SendFileAsync(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "damages", name));
    }
    
    [HttpPost]
    [Route("/api/transport/damage/send")]
    public IActionResult GetTransportTest()
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
}