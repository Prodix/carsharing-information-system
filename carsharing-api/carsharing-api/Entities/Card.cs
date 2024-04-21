using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("card")]
public class Card
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public string PaymentSystem { get; set; }
    public string CardNumber { get; set; }
    public string Cvc { get; set; }
    public DateOnly ExpireDate { get; set; }
}