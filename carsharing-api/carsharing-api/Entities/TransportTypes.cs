using NpgsqlTypes;
using System.Runtime.Serialization;


public enum CarType
{
    [PgName("base")]
    BASE,
    [PgName("comfort")]
    COMFORT,
    [PgName("business")]
    BUSINESS
}