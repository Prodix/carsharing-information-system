using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("message")]
public class UserMessage
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public DateTime Datetime { get; set; }
    public string Message { get; set; }
    public bool IsRead { get; set; }
}