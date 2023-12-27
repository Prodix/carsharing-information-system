using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Database.Models;

public class Code
{
    [Column("id")]
    public int Id { get; set; }
    [Column("email")]
    public string? Email { get; set; }
    [Column("code")]
    public int GeneratedCode { get; set; }
    [Column("creation_date")]
    public DateTime CreationDate { get; set; }
}