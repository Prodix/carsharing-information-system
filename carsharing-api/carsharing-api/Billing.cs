using System;
using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api
{
	public class Billing
	{
		[Column("id")]
		public int Id { get; set; }
		[Column("balance")]
		public int Balance { get; set; }
	}
}

