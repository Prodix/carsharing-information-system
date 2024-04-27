using NpgsqlTypes;

namespace carsharing_api.Entities;

public enum Action
{
    [PgName("reserve")]
    RESERVE,
    [PgName("cancel_reserve")]
    CANCEL_RESERVE,
    [PgName("rent")]
    RENT,
    [PgName("cancel_rent")]
    CANCEL_RENT,
    [PgName("check")]
    CHECK,
    [PgName("lock")]
    LOCK,
    [PgName("unlock")]
    UNLOCK
}