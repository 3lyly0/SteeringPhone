namespace SteeringPhone.Core.Profiles;

public class ProfilesManager
{
    private readonly List<GameProfile> _profiles = new();

    public ProfilesManager()
    {
        // Load default game presets
        _profiles.Add(new GameProfile { Name = "Euro Truck Simulator 2", GameName = "ETS2", MaxAngleDegrees = 900.0f, Sensitivity = 1.0f });
        _profiles.Add(new GameProfile { Name = "Forza Horizon 5", GameName = "Forza Horizon 5", MaxAngleDegrees = 180.0f, Sensitivity = 1.2f });
        _profiles.Add(new GameProfile { Name = "BeamNG.drive", GameName = "BeamNG", MaxAngleDegrees = 540.0f, Sensitivity = 1.0f });
        _profiles.Add(new GameProfile { Name = "Assetto Corsa", GameName = "Assetto Corsa", MaxAngleDegrees = 360.0f, Sensitivity = 1.0f });
    }

    public IReadOnlyList<GameProfile> GetProfiles() => _profiles.AsReadOnly();

    public GameProfile GetDefaultProfile() => _profiles[0];
}
