using Nefarius.ViGEm.Client;
using Nefarius.ViGEm.Client.Targets;
using Nefarius.ViGEm.Client.Targets.Xbox360;
using SteeringPhone.Core.Protocol;

namespace SteeringPhone.Core.Input;

/// <summary>
/// Manages the virtual Xbox 360 controller target via Nefarius.ViGEm.Client kernel driver integration.
/// </summary>
public class ViGEmControllerService
{
    private ViGEmClient? _client;
    private IXbox360Controller? _controller;

    public bool IsConnected { get; private set; }
    public string? ErrorMessage { get; private set; }

    public async Task<bool> InitializeAsync()
    {
        if (IsConnected) return true;

        if (!DriverInstaller.IsDriverInstalled())
        {
            await DriverInstaller.InstallDriverSilentlyAsync();
        }

        try
        {
            _client = new ViGEmClient();
            _controller = _client.CreateXbox360Controller();
            _controller.Connect();
            IsConnected = true;
            ErrorMessage = null;
            return true;
        }
        catch (Exception ex)
        {
            // Attempt silent driver installation recovery if missing driver exception
            if (await DriverInstaller.InstallDriverSilentlyAsync())
            {
                try
                {
                    _client = new ViGEmClient();
                    _controller = _client.CreateXbox360Controller();
                    _controller.Connect();
                    IsConnected = true;
                    ErrorMessage = null;
                    return true;
                }
                catch (Exception retryEx)
                {
                    ex = retryEx;
                }
            }

            IsConnected = false;
            ErrorMessage = ex.Message;
            return false;
        }
    }

    public bool Initialize()
    {
        return InitializeAsync().GetAwaiter().GetResult();
    }

    public void UpdateInput(DrivePacket packet)
    {
        if (!IsConnected || _controller == null) return;

        try
        {
            // Map Steering [-1.0, +1.0] -> Xbox LeftThumbX [-32768, +32767]
            short thumbX = InputMapper.MapSteeringToThumbstickX(packet.SteeringAngle);
            _controller.SetAxisValue(Xbox360Axis.LeftThumbX, thumbX);

            // Map Triggers: Throttle -> RightTrigger, Brake -> LeftTrigger
            byte rightTrigger = InputMapper.MapPedalToTrigger(packet.Throttle);
            byte leftTrigger = InputMapper.MapPedalToTrigger(packet.Brake);

            _controller.SetSliderValue(Xbox360Slider.RightTrigger, rightTrigger);
            _controller.SetSliderValue(Xbox360Slider.LeftTrigger, leftTrigger);

            // Map Buttons
            var mask = packet.ButtonMask;
            _controller.SetButtonState(Xbox360Button.A, mask.HasFlag(ButtonMask.HandBrake));
            _controller.SetButtonState(Xbox360Button.B, mask.HasFlag(ButtonMask.Reverse));
            _controller.SetButtonState(Xbox360Button.X, mask.HasFlag(ButtonMask.Nitro));
            _controller.SetButtonState(Xbox360Button.Y, mask.HasFlag(ButtonMask.Horn));
            _controller.SetButtonState(Xbox360Button.RightShoulder, mask.HasFlag(ButtonMask.GearUp));
            _controller.SetButtonState(Xbox360Button.LeftShoulder, mask.HasFlag(ButtonMask.GearDown));
            _controller.SetButtonState(Xbox360Button.Start, mask.HasFlag(ButtonMask.Pause));
            _controller.SetButtonState(Xbox360Button.Back, mask.HasFlag(ButtonMask.Menu));

            _controller.SubmitReport();
        }
        catch
        {
            // Ignore report error
        }
    }

    public void Disconnect()
    {
        try
        {
            if (_controller != null)
            {
                _controller.Disconnect();
                _controller = null;
            }
        }
        catch { }
        _client = null;
        IsConnected = false;
    }
}
