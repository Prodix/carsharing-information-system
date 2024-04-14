using carsharing_api.Context;
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
}