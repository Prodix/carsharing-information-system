using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;
using carsharing_api.Entities;

namespace carsharing_api.Context;

public class CarsharingDbContext : DbContext
{
    public CarsharingDbContext(DbContextOptions<CarsharingDbContext> options)
        : base(options)
    {
    }
    
    public DbSet<Transport> Transport { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.HasPostgresEnum("car_type", new[] { "base", "comfort", "business" });
    }
}
