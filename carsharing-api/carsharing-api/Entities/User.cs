using System.ComponentModel.DataAnnotations.Schema;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace carsharing_api.Entities;

[Table("user")]
[JsonObject(MemberSerialization = MemberSerialization.OptOut)]
public class User
{
    public int Id { get; set; }
    public string Email { get; set; }
    public string Password { get; set; }
    [JsonConverter(typeof(StringEnumConverter))]
    public Role UserRole { get; set; }
    public int? PassportId { get; set; }
    public double Balance { get; set; }
    public int? DriverLicenseId { get; set; }
    public int? SelfieId { get; set; }
    public bool IsVerified { get; set; }
    public bool IsEmailVerified { get; set; }
}