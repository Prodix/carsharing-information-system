using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Database.Models;

public class User
{
    [Column("id")]
    public int Id { get; set; }
    [Column("email")]
    public string? Email { get; set; }
    [Column("password")]
    public string? Password { get; set; }
    [Column("token")]
    public string? Token { get; set; }
    [Column("passport")]
    public int Passport { get; set; }
    [Column("driver_license")]
    public int DriverLicense { get; set; }
    [Column("billing")]
    public int Billing { get; set; }
}
