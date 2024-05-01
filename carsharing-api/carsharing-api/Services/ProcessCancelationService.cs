using carsharing_api.Context;
using carsharing_api.Entities;
using NtpClient;
using Action = carsharing_api.Entities.Action;

namespace carsharing_api.Services;

public class ProcessCancelationService : BackgroundService
{
    private CarsharingDbContext _db;
    private IServiceProvider _provider;
    private NtpConnection _ntpClient;

    public ProcessCancelationService(IServiceProvider provider)
    {
        _provider = provider;
        _ntpClient = new NtpConnection("time.google.com");
    }
    
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        using var scope = _provider.CreateScope();
        _db = scope.ServiceProvider
            .GetRequiredService<CarsharingDbContext>();
            
        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                var logs = _db.TransportLog.ToList();

                foreach (var userLogs in logs.GroupBy(x => x.UserId))
                {
                    var transportLogs = userLogs.OrderBy(x => x.Id).Reverse().ToList();
                    var time = _ntpClient.GetUtc();
                    if (transportLogs[0].Action is Action.RESERVE or Action.BEEP or Action.FLASH)
                    {
                        foreach (var log in transportLogs)
                        {
                            if (log.Action is not Action.RESERVE) continue;

                            if ((time - log.DateTime).Minutes >= 20)
                            {
                                _db.TransportLog.Add(new TransportLog()
                                {
                                    Action = Action.CANCEL_RESERVE,
                                    DateTime = log.DateTime.AddMinutes(20),
                                    RateId = log.RateId,
                                    TransportId = log.TransportId,
                                    UserId = log.UserId
                                });
                                await _db.SaveChangesAsync();
                            }

                            break;
                        }
                    } 
                    else if (transportLogs[0].Action is Action.CHECK && (time - transportLogs[0].DateTime).Minutes >= 35)
                    {
                        _db.TransportLog.Add(new TransportLog()
                        {
                            Action = Action.CANCEL_CHECK,
                            DateTime = transportLogs[0].DateTime.AddMinutes(35),
                            RateId = transportLogs[0].RateId,
                            TransportId = transportLogs[0].TransportId,
                            UserId = transportLogs[0].UserId
                        });
                        await _db.SaveChangesAsync();
                    }
                    else if (transportLogs[0].Action is Action.UNLOCK or Action.LOCK or Action.RENT)
                    {
                        var rate = _db.Rate.First(x => x.Id == transportLogs[0].RateId);
                        
                        if (rate.RateName != "Фикс") 
                            continue;

                        var lastRent = _db.RentHistory.Where(x => x.UserId == userLogs.Key)
                            .ToList()
                            .OrderBy(x => x.Id)
                            .Reverse()
                            .ToList()[0];

                        var rentHours = lastRent.RentTime.Hours;
                        
                        foreach (var log in transportLogs)
                        {
                            if (log.Action is not Action.RENT) continue;
                            
                            if ((time - log.DateTime).Hours >= rentHours)
                            {
                                _db.TransportLog.Add(new TransportLog()
                                {
                                    Action = Action.CANCEL_RENT,
                                    DateTime = log.DateTime.AddHours(rentHours),
                                    RateId = log.RateId,
                                    TransportId = log.TransportId,
                                    UserId = log.UserId
                                });

                                var user = _db.User.First(x => x.Id == log.UserId);
                                user.Balance -= lastRent.Price;
                                
                                _db.User.Update(user);
                                
                                await _db.SaveChangesAsync();
                            }

                            break;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }

            await Task.Delay(1000);
        }
    }
}