using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Database.Models;

public class Car
{
    [Column("id")]
    public int Id { get; set; }
    [Column("brand")]
    public string Brand { get; set; }
    [Column("model")]
    public string Model { get; set; }
    [Column("image_path")]
    public string ImagePath { get; set; }
    [Column("fuel_level")]
    public string FuelLevel { get; set; }
    [Column("car_plate")]
    public string CarPlate { get; set; }
    [Column("class")]
    public int Category { get; set; }
    [Column("params")]
    public string Params { get; set; }
    [Column("insurance")]
    public int Insurance { get; set; }
    [Column("rate")]
    public int Rate { get; set; }
    [Column("address")]
    public string Address { get; set; }
    [Column("reservation")]
    public int? Reservation { get; set; }
}