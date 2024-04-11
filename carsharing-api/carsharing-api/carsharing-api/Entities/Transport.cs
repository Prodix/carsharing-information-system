using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("transport")]
public class Transport
{
    public int Id { get; set; }

    public string CarName { get; set; } = null!;

    public string CarNumber { get; set; } = null!;

    public string CarImagePath { get; set; } = null!;

    public bool IsReserved { get; set; }

    public short GasLevel { get; set; }

    public bool HasInsurance { get; set; }

    public bool IsDoorOpened { get; set; }

    public float Longitude { get; set; }

    public float Latitude { get; set; }
}
