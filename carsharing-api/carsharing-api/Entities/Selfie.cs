using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("selfie")]
public class Selfie
{
    public int Id { get; set; }
    public string Path { get; set; }
}