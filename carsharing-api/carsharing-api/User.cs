using System.ComponentModel.DataAnnotations.Schema;
using System.Formats.Asn1;

namespace carsharing_api;

public class User
{
    [Column("id")]
    public int Id { get; set; }
    [Column("email")]
    public string Email { get; set; }
    [Column("password")]
    public string Password { get; set; }
    [Column("token")]
    public string Token { get; set; }
    [Column("passport")]
    public int Passport { get; set; }
    [Column("driver_license")]
    public int DriverLicense { get; set; }
    [Column("billing")]
    public int Billing { get; set; }
}
