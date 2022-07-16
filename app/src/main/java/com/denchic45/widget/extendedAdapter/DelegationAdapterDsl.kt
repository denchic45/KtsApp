package com.denchic45.widget.extendedAdapter

import com.denchic45.kts.domain.DomainModel

@DslMarker
annotation class ExtendedAdapterMarker

class DelegationAdapterDsl {

    @ExtendedAdapterMarker
    class DelegationAdapterBuilder {

        private lateinit var delegates: Array<out AdapterDelegate>
        private var extensions: Array<AdapterDelegateExtension> = arrayOf()
        private var onItemClickListener: (position: Int) -> Unit = {}
        var changePayload: (DomainModel, DomainModel) -> Any? = { _, _ -> null }

        fun build(): DelegationAdapterExtended {
            val delegationAdapter = DelegationAdapterExtended(*delegates, changePayload = changePayload)
            delegationAdapter.addOnItemClickListener(onItemClickListener)
            delegationAdapter.addExtensions(*extensions)
            return delegationAdapter
        }

        fun delegates(vararg delegates: AdapterDelegate) {
            this.delegates = delegates
        }

        fun delegates(delegates: List<AdapterDelegate>) {
            this.delegates = delegates.toTypedArray()
        }

        fun onClick(function: (position: Int) -> Unit) {
            this.onItemClickListener = function
        }

        fun extensions(block: ExtensionsBuilder.() -> Unit) {
            extensions = ExtensionsBuilder().apply(block).extensions.toTypedArray()
        }

    }

    @ExtendedAdapterMarker
    class ExtensionsBuilder {

        val extensions = mutableListOf<AdapterDelegateExtension>()

        fun add(adapterDelegateExtension: AdapterDelegateExtension) {
            extensions.add(adapterDelegateExtension)
        }
    }

}

fun adapter(builderDelegation: DelegationAdapterDsl.DelegationAdapterBuilder.() -> Unit): DelegationAdapterExtended {
    val delegationAdapterBuilder = DelegationAdapterDsl.DelegationAdapterBuilder()
    return delegationAdapterBuilder.apply(builderDelegation).build()
}