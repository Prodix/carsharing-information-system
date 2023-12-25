using System;
using Microsoft.EntityFrameworkCore;

namespace carsharing_api
{
	public class CarsharingDatabase : DbContext
	{
		public DbSet<User> user { get; set; }

		public CarsharingDatabase()
		{ }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
            => optionsBuilder.UseNpgsql($"Host={Environment.GetEnvironmentVariable("HOST")};Database={Environment.GetEnvironmentVariable("DATABASE")};Username={Environment.GetEnvironmentVariable("PG_USER")};Password={Environment.GetEnvironmentVariable("PG_PASSWORD")}");
    }
}

