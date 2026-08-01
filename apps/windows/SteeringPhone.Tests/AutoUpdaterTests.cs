using SteeringPhone.Core.Update;
using Xunit;

namespace SteeringPhone.Tests;

public class AutoUpdaterTests
{
    [Fact]
    public void TestCurrentVersionConstant()
    {
        Assert.Equal("1.0.0", AutoUpdater.CURRENT_VERSION);
    }
}
