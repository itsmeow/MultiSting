package dev.itsmeow.multisting;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = MultiSting.MODID)
@Mod(value = MultiSting.MODID)
public class MultiSting {

    public static final String MODID = "multisting";
    public static final Pair<ModConfiguration, ForgeConfigSpec> CONFIG = new ForgeConfigSpec.Builder().configure(ModConfiguration::new);
    public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(MODID, "stings");

    public MultiSting() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG.getRight());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(BeeStingsCapability.class, new BeeStingsCapability.Storage(), BeeStingsCapability::new);
    }

    @SubscribeEvent
    public static void onAttachCaps(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof BeeEntity) {
            event.addCapability(CAPABILITY_ID, new ICapabilitySerializable<INBT>() {
                
                private LazyOptional<BeeStingsCapability> instance = LazyOptional.of(BeeStingsCapability.INSTANCE::getDefaultInstance);

                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                    return cap == BeeStingsCapability.INSTANCE ? instance.cast() : LazyOptional.empty();
                }

                @Override
                public INBT serializeNBT() {
                    return BeeStingsCapability.INSTANCE.getStorage().writeNBT(BeeStingsCapability.INSTANCE, instance.orElseThrow(() -> new IllegalArgumentException()), null);
                }

                @Override
                public void deserializeNBT(INBT nbt) {
                    BeeStingsCapability.INSTANCE.getStorage().readNBT(BeeStingsCapability.INSTANCE, instance.orElseThrow(() -> new IllegalArgumentException()), null, nbt);
                }

            });
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LivingUpdateEvent event) {
        if(event.getEntity() instanceof BeeEntity && !event.getEntity().getEntityWorld().isRemote) {
            BeeEntity bee = (BeeEntity) event.getEntity();
            int stingConfig = CONFIG.getLeft().stingAmount.get();
            boolean stung = bee.hasStung();
            BeeStingsCapability cap = bee.getCapability(BeeStingsCapability.INSTANCE).orElse(null);
            if(cap != null) {
                // lastTickStung is serialized meaning this won't run on entities that are loaded
                if(!cap.lastTickStung && stung) {
                    cap.addSting();
                }
                cap.lastTickStung = stung;
                System.out.println(cap.stings + " / " + stingConfig);
                if(stingConfig == -1 && stung) {
                    bee.setHasStung(false);
                } else if(stingConfig == 0 && !stung) {
                    bee.setHasStung(true);
                    // don't call addSting by doing this
                    cap.lastTickStung = true;
                } else if(stingConfig > 1) {
                    if(cap.getStings() < stingConfig) {
                        bee.setHasStung(false);
                    } else {
                        bee.setHasStung(true);
                        // don't call addSting by doing this
                        cap.lastTickStung = true;
                    }
                }
            }
        }
    }

    public static class ModConfiguration {

        public ForgeConfigSpec.IntValue stingAmount;

        public ModConfiguration(ForgeConfigSpec.Builder builder) {
            this.stingAmount = builder.comment("How many times a bee can sting. -1 = infinite, 0 = never, 1 = normal behaviour").defineInRange("sting_amount", -1, -1, Integer.MAX_VALUE);
        }

    }

    public static class BeeStingsCapability {

        @CapabilityInject(BeeStingsCapability.class)
        public static Capability<BeeStingsCapability> INSTANCE;

        private int stings = 0;
        public boolean lastTickStung = false;

        public void addSting() {
            this.stings++;
        }

        public void setStings(int stings) {
            this.stings = stings;
        }

        public int getStings() {
            return stings;
        }

        private static class Storage implements Capability.IStorage<BeeStingsCapability> {

            @Override
            public INBT writeNBT(Capability<BeeStingsCapability> capability, BeeStingsCapability instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt("stings", instance.stings);
                nbt.putBoolean("lastTickStung", instance.lastTickStung);
                return nbt;
            }

            @Override
            public void readNBT(Capability<BeeStingsCapability> capability, BeeStingsCapability instance, Direction side, INBT nbt) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.setStings(tag.getInt("stings"));
                instance.lastTickStung = tag.getBoolean("lastTickStung");
            }
        }
    }
}
