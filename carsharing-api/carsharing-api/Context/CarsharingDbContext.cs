using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;
using carsharing_api.Entities;
using Npgsql;

namespace carsharing_api.Context;

public class CarsharingDbContext : DbContext
{
    public CarsharingDbContext(DbContextOptions<CarsharingDbContext> options)
        : base(options)
    {
    }

    public DbSet<Transport> Transport { get; set; }
    public DbSet<Function> Function { get; set; }
    public DbSet<Rate> Rate { get; set; }
    public DbSet<Damage> Damage { get; set; }

}
