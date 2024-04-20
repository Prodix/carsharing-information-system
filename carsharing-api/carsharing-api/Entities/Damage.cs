using System.ComponentModel.DataAnnotations.Schema;
using Newtonsoft.Json;

namespace carsharing_api.Entities;

[Table("damage")]
[JsonObject(MemberSerialization = MemberSerialization.OptOut)]
public class Damage
{
    public int Id { get; set; }
    
    public int TransportId { get; set; }
    
    public string Path { get; set; }
}