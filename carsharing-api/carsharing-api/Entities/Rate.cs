using System.ComponentModel.DataAnnotations.Schema;
using Newtonsoft.Json;

namespace carsharing_api.Entities;

[Table("rate")]
public class Rate
{
    public int Id { get; set; }
    public int TransportId { get; set; }
    public string RateName { get; set; }
    public double OnRoadPrice { get; set; }
    public double ParkingPrice { get; set; }
}