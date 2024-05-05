using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("penalty")]
public class Penalty
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public double Price { get; set; }
    public bool IsPaid { get; set; }
    public int RelatedRent { get; set; }
    public string Description { get; set; }
    public int RatingPenalty { get; set; }
}