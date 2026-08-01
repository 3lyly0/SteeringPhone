using SteeringPhone.Core.Profiles;
using Xunit;

namespace SteeringPhone.Tests;

public class ProfilesManagerTests
{
    [Fact]
    public void TestDefaultProfilesLoaded()
    {
        var manager = new ProfilesManager();
        var profiles = manager.GetProfiles();

        Assert.NotEmpty(profiles);
        Assert.Contains(profiles, p => p.Name.Contains("Euro Truck Simulator 2"));
        Assert.Contains(profiles, p => p.Name.Contains("Forza Horizon 5"));
    }
}
