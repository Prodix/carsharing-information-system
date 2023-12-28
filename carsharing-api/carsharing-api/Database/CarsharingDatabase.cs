using carsharing_api.Database.Models;
using Microsoft.EntityFrameworkCore;

namespace carsharing_api.Database
{
	public class CarsharingDatabase : DbContext
	{
		public DbSet<User> user { get; set; }
		public DbSet<Billing> billing { get; set; }
		public DbSet<DriverLicense> driver_license { get; set; }
		public DbSet<Passport> passport { get; set; }
		public DbSet<Code> codes { get; set; }
		public DbSet<Car> car { get; set; }

		public CarsharingDatabase()
		{ }

		public CarsharingDatabase(DbContextOptions<CarsharingDatabase> options)
			: base(options)
		{ }

		protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
		{
			var configuration = new ConfigurationBuilder().AddJsonFile("appsettings.json").Build();
			optionsBuilder.UseNpgsql(configuration.GetConnectionString("DefaultConnection"));
		}
	}
}

