using Microsoft.AspNetCore.Mvc;

namespace carsharing_api.Controllers;

public class MainController : Controller
{
    [HttpGet]
    [Route("/{name}")]
    public IActionResult Get(string name)
    {
        var path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "pages", $"{name}.html");
        
        if (!System.IO.File.Exists(path))
            return NotFound();
        
        return PhysicalFile(path, "text/html");
    }
}