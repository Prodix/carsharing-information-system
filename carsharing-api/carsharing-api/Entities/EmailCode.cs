using System.ComponentModel.DataAnnotations.Schema;
using System.Dynamic;

namespace carsharing_api.Entities;

[Table("email_code")]
public class EmailCode
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public string Code { get; set; }
    public DateTime DateTime { get; set; }
}