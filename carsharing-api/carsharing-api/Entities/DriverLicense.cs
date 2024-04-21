using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("driver_license")]
public class DriverLicense
{
    public int Id { get; set; }
    public string Path { get; set; }
}