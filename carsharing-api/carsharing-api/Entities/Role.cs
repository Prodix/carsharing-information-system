using NpgsqlTypes;

namespace carsharing_api.Entities;

public enum Role
{
    [PgName("user")]
    USER,
    [PgName("admin")]
    ADMIN
}