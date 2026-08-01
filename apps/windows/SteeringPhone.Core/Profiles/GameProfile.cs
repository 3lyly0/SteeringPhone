namespace SteeringPhone.Core.Profiles;

public class GameProfile
{
    public string Id { get; set; } = Guid.NewGuid().ToString();
    public string Name { get; set; } = "Default Profile";
    public string GameName { get; set; } = "Generic Racing";
    public float Sensitivity { get; set; } = 1.0f;
    public float Deadzone { get; set; } = 0.05f;
    public float MaxAngleDegrees { get; set; } = 90.0f;
    public float ExponentialCurve { get; set; } = 1.0f;
}
