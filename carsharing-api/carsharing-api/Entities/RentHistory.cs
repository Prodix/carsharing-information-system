using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("rent_history")]
public class RentHistory
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public int TransportId { get; set; }
    public Transport Transport { get; set; }
    public int RateId { get; set; }
    public TimeSpan RentTime { get; set; }
    public double Price { get; set; }
    public DateOnly Date { get; set; }
}