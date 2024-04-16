using NpgsqlTypes;

namespace carsharing_api.Entities;

public enum FunctionType
{
    [PgName("child_chair")]
    CHILD_CHAIR,
    [PgName("transponder")]
    TRANSPONDER
}