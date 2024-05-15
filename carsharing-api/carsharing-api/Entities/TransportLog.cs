using System.ComponentModel.DataAnnotations.Schema;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace carsharing_api.Entities;

[Table("transport_log")]
[JsonObject(MemberSerialization = MemberSerialization.OptOut)]
public class TransportLog
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public int TransportId { get; set; }
    [JsonConverter(typeof(StringEnumConverter))]
    public Action Action { get; set; }
    public DateTime DateTime { get; set; }
    public int? RateId { get; set; }
}