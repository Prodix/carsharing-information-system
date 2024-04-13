using carsharing_api.Context;
using Microsoft.EntityFrameworkCore;
using Npgsql;

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

        var connection = builder.Configuration.GetConnectionString("DefaultConnection");

        var dataSourceBuilder = new NpgsqlDataSourceBuilder(connection);
        dataSourceBuilder.MapEnum<CarType>();
        var dataSource = dataSourceBuilder.Build();

        builder.Services.AddDbContext<CarsharingDbContext>(options =>
        {
            options.UseNpgsql(dataSource).UseSnakeCaseNamingConvention();
        });

        var app = builder.Build();

        // Configure the HTTP request pipeline.
        if (app.Environment.IsDevelopment())
        {
            app.UseSwagger();
            app.UseSwaggerUI();
        }

        app.UseAuthorization();
        
        app.MapControllers();

        app.Run();
    }
}