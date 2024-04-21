using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Entities;

[Table("passport")]
public class Passport
{
    public int Id { get; set; }
    public string Path { get; set; }
}