package powercrystals.powerconverters.power.systems.ic2;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.powerconverters.power.PowerSystemManager;
import powercrystals.powerconverters.power.base.TileEntityEnergyConsumer;
import powercrystals.powerconverters.power.systems.PowerIndustrialcraft;

public class TileEntityIndustrialCraftConsumer extends TileEntityEnergyConsumer<IEnergyEmitter> implements IEnergySink {
    private boolean _isAddedToEnergyNet;
    private boolean _didFirstAddToNet;
    private double _euLastTick;
    private long _lastTickInjected;

    @SuppressWarnings("unused")
    public TileEntityIndustrialCraftConsumer() {
        this(0);
    }

    public TileEntityIndustrialCraftConsumer(int voltageIndex) {
        super(PowerSystemManager.getInstance().getPowerSystemByName(PowerIndustrialcraft.id), voltageIndex, IEnergyEmitter.class);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!_didFirstAddToNet && !worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            _didFirstAddToNet = true;
            _isAddedToEnergyNet = true;
        }

        if (worldObj.getWorldTime() - _lastTickInjected > 2) {
            _euLastTick = 0;
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (!_isAddedToEnergyNet) {
            _didFirstAddToNet = false;
        }
    }

    @Override
    public void invalidate() {
        if (_isAddedToEnergyNet) {
            if (!worldObj.isRemote) {
                MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            }
            _isAddedToEnergyNet = false;
        }
        super.invalidate();
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        return getTotalEnergyDemand() / getPowerSystem().getInternalEnergyPerInput();
    }

    @Override
    public int getSinkTier() {
        return this._voltageIndex;
    }

    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
        double pcuNotStored = storeEnergy(amount * getPowerSystem().getInternalEnergyPerInput(), false);
        double euNotStored = pcuNotStored / getPowerSystem().getInternalEnergyPerInput();

        double euThisInjection = (amount - euNotStored);

        if (_lastTickInjected == worldObj.getWorldTime()) {
            _euLastTick += euThisInjection;
        } else {
            _euLastTick = euThisInjection;
            _lastTickInjected = worldObj.getWorldTime();
        }

        return euNotStored;
    }

    @Override
    public double getInputRate() {
        return _euLastTick;
    }
}
