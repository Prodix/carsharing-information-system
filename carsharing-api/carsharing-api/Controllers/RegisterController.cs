using Microsoft.AspNetCore.Mvc;

namespace carsharing_api.Controllers;

[ApiController]
[Route("/api/auth/[controller]")]
public class RegisterController : ControllerBase
{
    CarsharingDatabase db;

    public RegisterController(CarsharingDatabase db)
    {
        this.db = db;
    }


    [HttpGet]
    public JsonResult Get()
    {
        return new JsonResult(db.user.ToList());
    }

    [HttpPost]
    public JsonResult Post()
    {
        return new JsonResult(new object());
    }




}

