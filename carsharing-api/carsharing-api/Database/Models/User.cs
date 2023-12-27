using System.ComponentModel.DataAnnotations.Schema;

namespace carsharing_api.Database.Models;

public class User
{
    [Column("id")]
    public int Id { get; set; }
    [Column("email")]
    public string? Email { get; set; }
    [Column("password")]
    public string? Password { get; set; }
    [Column("token")]
    public string? Token { get; set; }
    [Column("passport")]
    public int Passport { get; set; }
    [Column("driver_license")]
    public int DriverLicense { get; set; }
    [Column("billing")]
    public int Billing { get; set; }
}

/*
{
  "license": {
       "serie": 123,
       "number": 123,
       "start_date": "2023-12-23",
       "end_date": "2023-12-23",
       "city": "city",
       "categories": [
           "A"
       ]
   },
   "passport": {
       "serie": 123,
       "number": 123,
       "name": "123",
       "surname": "123",
       "patronymic": "123",
       "birth_date": "2023-12-12"
   },
   "user": {
       "email": "123",
       "password": "123"
   }
}
 */