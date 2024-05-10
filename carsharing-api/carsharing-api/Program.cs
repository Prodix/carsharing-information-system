using carsharing_api.Context;
using carsharing_api.Entities;
using carsharing_api.Services;
using Microsoft.EntityFrameworkCore;
using Npgsql;
using Action = carsharing_api.Entities.Action;

namespace carsharing_api;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        // Add services to the container.

        builder.Services.AddControllers();
        // Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen();
        builder.Services.AddMvc();
        
        AppContext.SetSwitch("Npgsql.EnableLegacyTimestampBehavior", true);

        var connection = builder.Configuration.GetConnectionString("DefaultConnection");

        var dataSourceBuilder = new NpgsqlDataSourceBuilder(connection);
        dataSourceBuilder.MapEnum<CarType>();
        dataSourceBuilder.MapEnum<FunctionType>();
        dataSourceBuilder.MapEnum<Role>();
        dataSourceBuilder.MapEnum<Action>();
        var dataSource = dataSourceBuilder.Build();

        builder.Services.AddDbContext<CarsharingDbContext>(options =>
        {
            options.UseNpgsql(dataSource).UseSnakeCaseNamingConvention();
        });
        
        builder.Services.AddHostedService<ProcessCancelationService>();

        var app = builder.Build();

        // Configure the HTTP request pipeline.
        if (app.Environment.IsDevelopment())
        {
            app.UseSwagger();
            app.UseSwaggerUI();
        }

        app.UseAuthorization();
        
        app.MapControllers();

        app.UseCors(corsBuilder => corsBuilder.AllowAnyHeader().AllowAnyMethod().AllowAnyOrigin());
        
        app.Run();
    }
}