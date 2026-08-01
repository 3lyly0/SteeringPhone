using SteeringPhone.Core.Input;
using Xunit;

namespace SteeringPhone.Tests;

public class InputMapperTests
{
    [Theory]
    [InlineData(0.0f, 0)]
    [InlineData(1.0f, 32767)]
    [InlineData(-1.0f, -32767)]
    [InlineData(0.5f, 16383)]
    [InlineData(2.0f, 32767)]
    [InlineData(-2.0f, -32767)]
    public void TestSteeringAxisMapping(float inputAngle, short expectedAxis)
    {
        short result = InputMapper.MapSteeringToThumbstickX(inputAngle);
        Assert.Equal(expectedAxis, result);
    }

    [Fact]
    public void TestPedalToTriggerMapping()
    {
        byte triggerVal = InputMapper.MapPedalToTrigger(255);
        Assert.Equal(255, triggerVal);
    }
}
