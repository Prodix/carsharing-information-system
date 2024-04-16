using System.ComponentModel.DataAnnotations.Schema;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace carsharing_api.Entities;

[Table("function")]
[JsonObject(MemberSerialization = MemberSerialization.OptOut)]
public class Function
{
    public int Id { get; set; }
    
    public int TransportId { get; set; }
    
    [JsonConverter(typeof(StringEnumConverter))]
    public FunctionType FunctionData { get; set; }
}