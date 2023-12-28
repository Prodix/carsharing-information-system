using carsharing_api.Database;
using Microsoft.AspNetCore.Mvc;

namespace carsharing_api.Controllers;

[ApiController]
public class CarController : Controller
{
    private CarsharingDatabase _db;

    public CarController(CarsharingDatabase db)
    {
        _db = db;
    }

    [HttpGet]
    [Route("/api/car/get")]
    public JsonResult GetCars()
    {
        return new JsonResult(_db.car.ToList());
    }
}