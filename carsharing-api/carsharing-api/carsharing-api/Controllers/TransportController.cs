﻿using carsharing_api.Context;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

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
    public string GetTransport()
    {
        return JsonConvert.SerializeObject(_db.Transport.ToList(), Formatting.Indented);
    }
}