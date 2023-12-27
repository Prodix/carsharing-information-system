using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Database.Models
{
	public class DriverLicense
	{
        [Column("id")]
        public int Id { get; set; }
        [Column("serie")]
        public int Serie { get; set; }
        [Column("number")]
        public int Number { get; set; }
        [Column("start_date")]
        public DateTime StartDate { get; set; }
        [Column("end_date")]
        public DateTime EndDate { get; set; }
        [Column("city")]
        public string? City { get; set; }
        [Column("categories")]
        public string[]? Categories { get; set; }
	}
}

