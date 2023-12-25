using System;
using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api
{
	public class Passport
	{
        [Column("id")]
		public int Id { get; set; }
        [Column("serie")]
        public int Serie { get; set; }
        [Column("number")]
        public int Number { get; set; }
        [Column("name")]
        public string Name { get; set; }
        [Column("surname")]
        public string Surname { get; set; }
        [Column("patronymic")]
        public string? Patronymic { get; set; }
        [Column("birth_date")]
        public DateTime BirthDate { get; set; }
	}
}

