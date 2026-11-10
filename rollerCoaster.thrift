
namespace py rollerCoaster
namespace java rollerCoaster


service Station
{
    // Passengers
    i32 i_am_boarding(1: i32 passengerId),
    void wait_for_ride_end(1: i32 passengerId),
    void i_am_disembarking(1: i32 passengerId)

    // Wagons
    void arrive(1: i32 wagonId),
    void departure(1: i32 wagonId),
    void ride_finished(1: i32 wagonId)
}