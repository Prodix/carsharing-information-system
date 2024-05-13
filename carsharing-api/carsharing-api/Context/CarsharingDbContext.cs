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
    public DbSet<Card> Card { get; set; }
    public DbSet<Penalty> Penalty { get; set; }
    public DbSet<DriverLicense> DriverLicense { get; set; }
    public DbSet<EmailCode> EmailCode { get; set; }
    public DbSet<TransportLog> TransportLog { get; set; }
    public DbSet<RentHistory> RentHistory { get; set; }
    public DbSet<Passport> Passport { get; set; }
    public DbSet<Selfie> Selfie { get; set; }
    public DbSet<User> User { get; set; }
    public DbSet<Function> Function { get; set; }
    public DbSet<Rate> Rate { get; set; }
    public DbSet<Damage> Damage { get; set; }
    public DbSet<UserMessage> Message { get; set; }
}
